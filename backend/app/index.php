<?php

use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

use \Graeuler\Garden\Collect\DataFeedDatastore as DataFeedDatastore;
use \Graeuler\Garden\Collect\UplinkDataProcessor as UplinkDataProcessor;
use \Graeuler\Garden\Collect\InvalidDataException as InvalidDataException;
use \Graeuler\Garden\Collect\ApiToken as ApiToken;
use \Graeuler\Garden\Collect\InvalidTokenException as InvalidTokenException;

require '../vendor/autoload.php';

function getJsonErrorConstant($errorCode) {
    $constants = get_defined_constants(true);
    foreach ($constants["json"] as $name => $value) {
        if (!strncmp($name, "JSON_ERROR_", 11)) {
            $json_errors[$value] = $name;
        }
    }
    return $json_errors[$errorCode];
}

$dbConnection = new PDO('sqlite:../data.sqlite');

$container = new \Slim\Container;
$container['dataStore'] = new DataFeedDatastore($dbConnection);
$container['uplinkDataProcessor'] = new UplinkDataProcessor();
$container['apiToken'] = new ApiToken();

$app = new \Slim\App($container);

$app->post('/collect/[{source}]', function (Request $request, Response $response, $arguments) {

    $source = $arguments['source'];

    $dataProcessor = $this->get('uplinkDataProcessor');
    $dataStore = $this->get('dataStore');
    $apiToken = $this->get('apiToken');
    
    $r = new stdClass();
    $r->success = false;
    $parsedBody = $request->getParsedBody();
    if (is_null($parsedBody) || ! is_array($parsedBody)) {
        $r->message = sprintf('Error decoding body. %s (%s)', json_last_error_msg(), getJsonErrorConstant(json_last_error()));
        $responseCode = 400;
    } else {
        try {
            $validTokens = $dataStore->selectTokenBySource($source, $parsedBody);
            $apiToken->check($parsedBody, $validTokens); // throws InvalidTokenException
            $dataSet = $dataProcessor->buildDataSet($parsedBody, $source);
            $r->success = $dataStore->storeDataSet($dataSet);
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
    return $response->withJson($r, $responseCode);
});

$app->run();
