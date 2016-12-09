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

        $parsedBody = $request->getParsedBody();

        if (is_null($parsedBody) || ! is_array($parsedBody)) {
            $r->success = false;
            $r->message = sprintf('Error decoding body. %s (%s)', json_last_error_msg(), 
                    $this->getJsonErrorConstant(json_last_error()));
            $responseCode = 400;
        } else {
            try {
                $validTokens = $this->dataStore->selectTokenBySource($source, $parsedBody);
                $apiToken->checkJsonData($parsedBody, $validTokens); // throws InvalidTokenException
                $dataSet = $dataProcessor->buildDataSet($parsedBody, $source);
                $r->success = $this->dataStore->storeDataSet($dataSet);
                $responseCode = 200;
            } catch (InvalidDataException $ide) {
                $responseCode = $this->setClientError($r, $ide->getMessage());
            } catch (InvalidTokenException $ite) {
                $responseCode = $this->setClientError($r, $ite->getMessage(), 403);
            } catch (\PDOException $pdoe) {
                $responseCode = $this->setServerError($r, $pdoe->getMessage());
                $r->code = $pdoe->getCode();
            } 
        } 
        return $response->withJson($r, $responseCode, JSON_PRETTY_PRINT);
    }

    private function setErrorResponse(&$responseContent, $message, $httpStatusCode) {
        $responseContent->success = false;
        $responseContent->message = $message;
        return $httpStatusCode;
    }
    
    private function setClientError(&$responseContent, $message, $httpStatusCode = 400) {
        $this->setErrorResponse($responseContent, $message, $httpStatusCode);
    }
    
    private function setServerError(&$responseContent, $message, $httpStatusCode = 500) {
        $this->setErrorResponse($responseContent, $message, $httpStatusCode);
    }

    private function getJsonErrorConstant($errorCode) {
        $constants = get_defined_constants(true);
        foreach ($constants["json"] as $name => $value) {
            if (!strncmp($name, "JSON_ERROR_", 11)) {
                $json_errors[$value] = $name;
            }
        }
        return $json_errors[$errorCode];
    }

}