<?php

namespace Graeuler\Garden\Collect;

use \Interop\Container\ContainerInterface;

class BaseController 
{
    protected $ci;
    protected $dataStore;
    
    public function __construct (ContainerInterface $ci) {
        $this->ci = $ci;
        $this->dataStore = $ci->get('dataStore');
//        $this->apiToken = $this->get('apiToken');
    }
    
}