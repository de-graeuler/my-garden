<?php

namespace Graeuler\Garden\Collect;

use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Http\Message\ResponseInterface as Response;

class ReportController extends BaseController
{
    public function keys (Request $request, Response $response, $arguments) 
    {
        $pSource   = $arguments['source'];
        $data = $this->dataStore->selectKeysBySource($pSource);
        return $response->withJson($data, 200, JSON_PRETTY_PRINT);
    }
    
    public function fetch (Request $request, Response $response, $arguments) 
    {
        $pSource   = $arguments['source'];
        $pKey      = $arguments['key'];
//        $pDateFrom = $arguments['iso_datetime_from'];
//        $pDateTo   = $arguments['iso_datetime_to'];
        
//        $pApiToken = $arguments['apitoken'];
                
        $r = new \stdClass();
        try {
//            $this->checkApiToken($pApiToken)
            $data = $this->dataStore->selectDataByKey($pSource, $pKey);
            foreach ($data as &$record) {
                switch($record['datatype']) {
                    case "double": $record['value'] = (float) $record['realdata']; break;
                    case "int":    $record['value'] =   (int) $record['intdata'];  break;
                    case "string": $record['value'] =         $record['stringdata'];   break;
                    default: $record['value'] = null; break;
                    { 
                          if (is_string($record['stringdata'])) $record['value'] =         $record['stringdata'];
                      elseif (is_int   ($record['intdata']))    $record['value'] =   (int) $record['intdata'];
                      elseif (is_float ($record['realdata']))   $record['value'] = (float) $record['realdata'];
                        else $record['value'] = null;
                    } break;
                }
                unset ($record['realdata']); 
                unset ($record['intdata']); 
                unset ($record['stringdata']);
                unset ($record['datatype']);
            }
            unset ($record);
            $r = $data;
        } catch (InvalidTokenException $ite) {
            $r->success = false;
            $r->message = $ite->getMessage();
            $responseCode = 403;
        }  catch (PDOException $pdoe) {
                $r->success = false;
                $r->message = $pdoe->getMessage();
                $r->code = $pdoe->getCode();
                $responseCode = 500;
        } 
        return $response->withJson($r, 200, JSON_PRETTY_PRINT);
    }

    protected function checkApiToken ($tokenHash) {
        $validTokens = $this->dataStore->selectTokenBySource($source, $parsedBody);
        $this->apiToken->check($pApiToken, $validTokens);
    }
    
}