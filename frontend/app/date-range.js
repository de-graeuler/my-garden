class DateRange {

    constructor() {
        this.start = moment().startOf('day').subtract(2, 'days');
        this.end = moment().endOf('day');
    }

    onDateRangeChanged(startDate, endDate) {
        // method stub, replace with event handler.
    }

    update (lowDate, highDate) {
        this.start = moment(lowDate).startOf('day');
        this.end = moment(highDate).endOf('day');
        this.publish();
    };

    shift (days) {
        this.start.add(days, 'days');
        this.end.add(days, 'days');
        this.publish();
    };

    extend (days) {
        if(days > 0 && this.end.isBefore(moment().subtract(days))) {
            this.end.add(days, 'days');
        } else {
            this.start.add(days, 'days');
        }
        this.publish();
    };

    shrink (days) {
        if(days > 0) {
            this.start.add(days, 'days');
        } else {
            his.end.add(days, 'days');
        }
        this.publish();
    };

    publish () {
        this.onDateRangeChanged(moment(this.start), moment(this.end));
    };

};