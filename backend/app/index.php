<?php

use Graeuler\Garden\Collect\DataFeedDatastore as DataFeedDatastore;
use Graeuler\Garden\Collect\UplinkDataProcessor as UplinkDataProcessor;
use Graeuler\Garden\Collect\ApiToken as ApiToken;
use Graeuler\Garden\Collect\JsonGUnzipMiddleware as JsonGUnzipMiddleware;

require '../vendor/autoload.php';

$config = json_decode(file_get_contents('../res/app.config.json'));

if ( is_null($config) ) {
    die("Config not parseable");
}

switch ($config->database->type) {
    case 'mysql': $dbConnection = new PDO(sprintf('mysql:host=%s;dbname=%s', 
                                            $config->database->host, $config->database->dbname ),
                                            $config->database->username, $config->database->password ); 
                  break;
    default:      $dbConnection = new PDO('sqlite:../data.sqlite'); break;
}

$container = new \Slim\Container;
$container['dataStore'] = new DataFeedDatastore($dbConnection);
$container['uplinkDataProcessor'] = new UplinkDataProcessor();
$container['apiToken'] = new ApiToken();

$jsonGUnzipMiddleware = new JsonGUnzipMiddleware();

$app = new \Slim\App($container);

// DEBUG:
// unset($app->getContainer()['errorHandler']);

$app->get('/', function($request, $response, $args) {
	$body = $response->getBody();
	$body->write(file_get_contents('frontend.html'));
	return $response;
});

$app->get('/report/v01/sources',
          '\Graeuler\Garden\Collect\ReportController:sources');
$app->get('/report/v01/keys/{source}', 
          '\Graeuler\Garden\Collect\ReportController:keys');
$app->get('/report/v01/fetch/{source}/{key}[/{iso_datetime_from}[/{iso_datetime_to}]]',
          '\Graeuler\Garden\Collect\ReportController:fetch');

$app->post('/collect/{source}', 
           '\Graeuler\Garden\Collect\CollectController:collect')
           ->add($jsonGUnzipMiddleware);

$app->run();
