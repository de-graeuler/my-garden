<?php

namespace Graeuler\Garden\Collect;

use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Http\Message\ResponseInterface as Response;

class CollectController extends BaseController
{

    public function collect (Request $request, Response $response, $arguments) {
    
        $source = $arguments['source'];
    
        $dataProcessor = $this->ci->get('uplinkDataProcessor');
        $apiToken = $this->ci->get('apiToken');
        
        $r = new \stdClass();
        $r->success = true; // as long as success is true the program can continue.

        if ("gzip" === $request->getHeaderLine('HTTP_CONTENT_ENCODING')) {
            // http://php.net/manual/de/function.gzuncompress.php#112202
            if ( false === ( $content = file_get_contents('php://input') ) ) {
                $this->setClientError($r, "Unable to read raw POST data.");
            }
            elseif ( false === ( $data = @gzinflate(substr(,10,-8)) ) ) {
                $this->setClientError($r, "Unable to decompress data.");
            } else {
                $parsedBody = json_decode($data, true);
            }
        } else {
            $parsedBody = $request->getParsedBody();
        } 

        if (is_null($parsedBody) || ! is_array($parsedBody)) {
            $r->message = sprintf('Error decoding body. %s (%s)', json_last_error_msg(), getJsonErrorConstant(json_last_error()));
            $responseCode = 400;
        } else {
            try {
                $validTokens = $this->dataStore->selectTokenBySource($source, $parsedBody);
                $apiToken->checkJsonData($parsedBody, $validTokens); // throws InvalidTokenException
                $dataSet = $dataProcessor->buildDataSet($parsedBody, $source);
                $r->success = $this->dataStore->storeDataSet($dataSet);
                $responseCode = 200;
            } catch (InvalidDataException $ide) {
                $this->setClientError($r, $ide->getMessage());
            } catch (InvalidTokenException $ite) {
                $this->setClientError($r, $ite->getMessage(), 403);
            } catch (PDOException $pdoe) {
                $this->setServerError($r, $ide->getMessage());
                $r->code = $pdoe->getCode();
            } 
        } 
        return $response->withJson($r, $responseCode, JSON_PRETTY_PRINT);
    }

    private function setErrorResponse(&$responseContent, $message, $httpStatusCode) {
        $responseContent->success = false;
        $responseContent->responseCode = 400;
        $responseContent->message = $message;
    }
    
    private function setClientError(&$responseContent, $message, $httpStatusCode = 400) {
        $this->setErrorResponse($responseContent, $message, $httpStatusCode)
    }
    
    private function setServerError(&$responseContent, $message, $httpStatusCode = 500) {
        $this->setErrorResponse($responseContent, $message, $httpStatusCode)
    }

}