class DataLoader {

    constructor(baseUrl, keys) {
        this.baseUrl = baseUrl;
        this.keys = keys;

        this.MAX_WATER_VOLUME  = 600;  // liters 
        this.WATER_LEVEL_WHEN_FULL = 21.0; // water level distance
        this.WATER_VOLUME_FACTOR = 8.7; 
    }

    onLoadComplete(payload, key) {
        // method stub, replaced with event listener
    }

    load(payload) {
        this.keys.forEach(key => {
            const url = this.baseUrl + key
            + '/' + encodeURIComponent(payload.start.toISOString(true))
            + '/' + encodeURIComponent(payload.end.toISOString(true));
            events.publish('data-loader/before', {request: payload, key: key});
            events.publish(payload.responseChannel, {request: payload, key: key});
            $.getJSON(url)
                .always(this.onLoadComplete(payload, key))
                .done((data) => events.publish(payload.responseChannel, this._buildResult(payload, key, data)))
                .fail((jqXHR, textStatus, errorThrown) => events.publish('data-loader/error', {
                    xhr: jqXHR, status: textStatus, error: errorThrown, request: payload
                  }));
        });
    }

    _buildResult(payload, key, data) {
        var result = {
            request: payload,
            key: key,
            values:  this._transform(key, data)
        }
        return result;
    }

    _transform(key, data) {
        var result = [];
        data.forEach(item => {
            result.push({
                t: item.isodatetime,
                y: this._transformValue(key, item.value)
            });
        });
        return result;
    }

    _transformValue(key, value) {
      if (key == 'water-lvl-distance') {
          return this._calcWaterVolume(value);
      }
      return value;
    }

    _calcWaterVolume ( waterLevel ) {
      return this.MAX_WATER_VOLUME - (waterLevel -this. WATER_LEVEL_WHEN_FULL) * this.WATER_VOLUME_FACTOR;
    }

  
}