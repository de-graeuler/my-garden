<?php

use Graeuler\Garden\Collect\DataFeedDatastore as DataFeedDatastore;
use Graeuler\Garden\Collect\UplinkDataProcessor as UplinkDataProcessor;
use Graeuler\Garden\Collect\ApiToken as ApiToken;
use Graeuler\Garden\Collect\JsonGUnzipMiddleware as JsonGUnzipMiddleware;

require '../vendor/autoload.php';

$dbConnection = new PDO('sqlite:../data.sqlite');

$container = new \Slim\Container;
$container['dataStore'] = new DataFeedDatastore($dbConnection);
$container['uplinkDataProcessor'] = new UplinkDataProcessor();
$container['apiToken'] = new ApiToken();

$jsonGUnzipMiddleware = new JsonGUnzipMiddleware();

$app = new \Slim\App($container);

// DEBUG:
// unset($app->getContainer()['errorHandler']);

$app->get('/report/v01/keys/{source}', 
          '\Graeuler\Garden\Collect\ReportController:keys');
$app->get('/report/v01/fetch/{source}/{key}[/{iso_datetime_from}[/{iso_datetime_to}]]',
          '\Graeuler\Garden\Collect\ReportController:fetch');

$app->post('/collect/{source}', 
           '\Graeuler\Garden\Collect\CollectController:collect')
           ->add($jsonGUnzipMiddleware);

$app->run();
