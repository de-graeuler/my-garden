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
        $r->success = false;
        $parsedBody = $request->getParsedBody();
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
                $r->success = false;
                $r->message = $ide->getMessage();
                $responseCode = 400;
            } catch (InvalidTokenException $ite) {
                $r->success = false;
                $r->message = $ite->getMessage();
                $responseCode = 403;
            } catch (PDOException $pdoe) {
                $r->success = false;
                $r->message = $pdoe->getMessage();
                $r->code = $pdoe->getCode();
                $responseCode = 500;
            } 
        } 
        return $response->withJson($r, $responseCode, JSON_PRETTY_PRINT);
    }
    
}