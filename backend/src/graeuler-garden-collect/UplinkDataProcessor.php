<?php

namespace Graeuler\Garden\Collect;

class UplinkDataProcessor implements DataProcessorInterface 
{
    const API_TOKEN_JSON_KEY = "api-token";
    private function fail($message) {
        throw new InvalidDataException('Data Processing Error: ' . $message);
    }
       
    // Interface function 
    public function buildDataSet(array $jsonData, $source) {
        if ( ! ("garden" == strtolower($source) ) )  {
            $this->fail('Currently supports processing of garden data.'); }
        $dataSet = new GardenDataSet();
        foreach($jsonData as $key => $data) {
            if ($key === self::API_TOKEN_JSON_KEY) { 
                continue; }
            if ( ! is_array($data) ) {
                $this->fail(sprintf('Value for type %s has to be an array.', $key)); }
            foreach($data as $set) {
                if ( ! isset($set['t'] ) ) $this->fail (sprintf('IsoDateTime (t) missing in %s', $key));
                if ( ! isset($set['v'] ) ) $this->fail (sprintf('Value (v) missing in %s', $key));
                $dataSet->addValue($source, $key,  $set['t'], $set['v']);
            }
        }
        return $dataSet;
    }
        
}
