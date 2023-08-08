package org.tests.rawsql.transport;

public class QuerySumResponse {
    private long count;
    private Double sum;

    public QuerySumResponse() {
    }

    public QuerySumResponse(long count, Double sum) {
        this.count = count;
        this.sum = sum;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }
}
