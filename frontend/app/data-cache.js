class DataCache {

    constructor() {
        this.start = moment();
        this.end = moment();
        this.data = new Map();
        this.requestSubscriptions = new Map();
        this.currentRequestId = 0;
    }

    onDataRequest() {
        // stub: replace with event handling
    }

    onCacheStale() {
        // stub: replace with event handling
    }

    onCacheUpToDate() {
        // stub: replace with event handling
    }

    getKeyValueMapInRange(requestedRange) {
        var result = new Map();
        this.data.forEach((values, key)  => {
            var resultValues = [];
            values.forEach((element) => {
                if(moment(element.t).isBetween(requestedRange.start, requestedRange.end)) {
                    resultValues.push(element);
                }
            });
            result.set(key, resultValues);
        });
        return result;
    }

    updateDateRange(range) {
        var requestsFired = false;
        this.onCacheStale();
        if(this.start.isAfter(range.start, 'day')) {
            this._fireRequest(range.start, this.start.subtract(1, 'second'))
            requestsFired = true;
        } 
        if(this.end.isBefore(range.end, 'day')) {
            this._fireRequest(this.end.add(1, 'second'), range.end);
            requestsFired = true;
        }
        if(!requestsFired) {
            this.onCacheUpToDate();
        }
    }

    _fireRequest(rangeStart, rangeEnd) {
        const responseChannel = this._createResponseChannelSubscription();
        this.onDataRequest(responseChannel, rangeStart, rangeEnd);
    }

    _createResponseChannelSubscription() {
        const responseChannel = 'data-cache/temp/response' + ++this.currentRequestId;
        const subscription = events.subscribe(responseChannel, (responseData) => this._handleDataResponse(responseData));
        this.requestSubscriptions.set(responseChannel, { sub: subscription, keys: [] });
        return responseChannel;
    }

    _handleDataResponse(responseData) {
        const subscription = this.requestSubscriptions.get(responseData.request.responseChannel);
        if(responseData.values === undefined) {
            subscription.keys.push(responseData.key);
        } else {
            this._updateStartTime(responseData.request.start);
            this._updateEndTime(responseData.request.end);
            var values = this._getOrCreateValues(responseData.key); 
            var v_idx = 0;
            var element = responseData.values.shift();
            while(element !== undefined && responseData.values.length > 0 && v_idx < values.length) {
                var value = values[v_idx];
                while(element !== undefined && value.t > element.t) {
                    values.splice(v_idx++, 0, element);
                    element = responseData.values.shift();
                }
            }
            if(element !== undefined) {
                values.push(element);
            }
            if(responseData.values.length > 0) {
                Array.prototype.push.apply(values, responseData.values);
            }
            subscription.keys.splice(subscription.keys.indexOf(responseData.key), 1);
            if(subscription.keys.length == 0) {
                this._terminateSubscription(responseData.request.responseChannel, subscription);
                if(this.requestSubscriptions.size == 0) {
                    this.onCacheUpToDate();
                }
            }
        }
    }

    _getOrCreateValues(key) {
        if (this.data.has(key)) {
            return this.data.get(key);
        }
        var values = [];
        this.data.set(key, values);
        return values;
    }

    _terminateSubscription(responseChannel, subscription) {
        this.requestSubscriptions.delete(responseChannel);
        subscription.sub.remove();
    }

    _updateStartTime(startTime) {
        if (this.start.isAfter(startTime)) {
            this.start = startTime;
        }
    }

    _updateEndTime(endTime) {
        if (this.end.isBefore(endTime)) {
            this.end = endTime;
        }
    }

}
