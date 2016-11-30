<?php

namespace Graeuler\Garden\Collect;

class BaseController 
{
    protected $ci;
    protected $dataStore;
    
    public function __construct (\Interop\Container\ContainerInterface $ci) {
        $this->ci = $ci;
        $this->dataStore = $ci->get('dataStore');
//        $this->apiToken = $this->get('apiToken');
    }
    
}