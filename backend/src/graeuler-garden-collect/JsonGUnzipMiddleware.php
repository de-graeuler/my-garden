<?php

namespace Graeuler\Garden\Collect;

use Psr\Http\Message\ServerRequestInterface as Request;
use Psr\Http\Message\ResponseInterface as Response;

/**
 * This Slim Middleware registers a media type parser for gzip compressed json data.
 **/
class JsonGUnzipMiddleware {

    public function __invoke(Request $request, Response $response, $next) {
        $request->registerMediaTypeParser(
            "application/gzip", 
            function($input) {
                return json_decode(@gzinflate(substr($input,10,-8)), true);
            }
        );
        return $next($request, $response);
    }
    
}    
    