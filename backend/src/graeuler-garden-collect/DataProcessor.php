<?php

namespace Graeuler\Garden\Collect;

interface DataProcessor
{
    /**
     * @throws InvalidDataException If jsonData could not be translated into a DataSet instance.
     * 
     * @return DataSet A DataSet implementation representing the given json data structure.
     **/
     public function buildDataSet(array $jsonData, $source = null);
}

