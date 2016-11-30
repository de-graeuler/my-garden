<?php

use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Http\Message\ResponseInterface as Response;

use Graeuler\Garden\Collect\DataFeedDatastore as DataFeedDatastore;
use Graeuler\Garden\Collect\UplinkDataProcessor as UplinkDataProcessor;
use Graeuler\Garden\Collect\InvalidDataException as InvalidDataException;
use Graeuler\Garden\Collect\ApiToken as ApiToken;
use Graeuler\Garden\Collect\InvalidTokenException as InvalidTokenException;
use Graeuler\Garden\Collect\ReportController as ReportController;

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
// DEBUG:
unset($app->getContainer()['errorHandler']);

$app->get('/report/v01/keys/{source}', 
          '\Graeuler\Garden\Collect\ReportController:keys');
$app->get('/report/v01/fetch/{source}/{key}[/{iso_datetime_from}[/{iso_datetime_to}]]',
          '\Graeuler\Garden\Collect\ReportController:fetch');
$app->post('/collect/{source}', 
           '\Graeuler\Garden\Collect\CollectController:collect');
          
$app->run();
