<?php

namespace Graeuler\Garden\Collect;

interface DataProcessor
{
    /**
     * @param array $jsonData A php array extracted by json_decode on a string.
     * @param $source hints the data processor to read $jsonData correctly.
     *
     * @throws InvalidDataException If jsonData could not be translated into a DataSet instance.
     * 
     * @return DataSet A DataSet implementation representing the given json data structure.
     **/
     public function buildDataSet(array $jsonData, $source);
}

