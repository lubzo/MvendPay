package model;

/**
 * Created by tlubega on 9/15/2017.
 */

public class Wallet {
    private String phoneNumber;
    private String status;
    private String walletItemID;
    private String networkName;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWalletItemID() {
        return walletItemID;
    }

    public void setWalletItemID(String walletItemID) {
        this.walletItemID = walletItemID;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }
}
