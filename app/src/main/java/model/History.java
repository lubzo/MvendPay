package model;

/**
 * Created by tlubega on 9/28/2017.
 */

public class History {

    private String date;
    private String Amount;
    private String type;

    private String name;

    private String payer_id;
    private String payee_id;


    private String request_ref;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getPayer_id() {
        return payer_id;
    }

    public void setPayer_id(String payer_id) {
        this.payer_id = payer_id;
    }

    public String getPayee_id() {
        return payee_id;
    }

    public void setPayee_id(String payee_id) {
        this.payee_id = payee_id;
    }
    public String getRequest_ref() {
        return request_ref;
    }

    public void setRequest_ref(String request_ref) {
        this.request_ref = request_ref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
