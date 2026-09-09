package com.minecraft.lobby.menu.shop.pix;

public class PixData {

    private String paymentId;
    private String qrCode;

    public PixData(String paymentId, String qrCode) {
        this.paymentId = paymentId;
        this.qrCode = qrCode;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public boolean isPending() {
        return true; // placeholder
    }
}
