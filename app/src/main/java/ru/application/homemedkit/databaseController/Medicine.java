package ru.application.homemedkit.databaseController;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicines")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String cis;
    public String productName;
    public long expDate;
    public String prodFormNormName;
    public String prodDNormName;
    public double prodAmount;
    public String phKinetics;
    public String comment;
    @Embedded
    public Technical technical;

    public Medicine() {
    }

    @Ignore
    public Medicine(long id) {
        this.id = id;
    }

    @Ignore
    public Medicine(String cis,
                    String productName,
                    long expDate,
                    String prodFormNormName,
                    String prodDNormName,
                    double prodAmount,
                    String phKinetics,
                    String comment,
                    Technical technical) {
        this.cis = cis;
        this.productName = productName;
        this.expDate = expDate;
        this.prodFormNormName = prodFormNormName;
        this.prodDNormName = prodDNormName;
        this.prodAmount = prodAmount;
        this.phKinetics = phKinetics;
        this.comment = comment;
        this.technical = technical;
    }


    @Ignore
    public Medicine(String productName, long expDate) {
        this.productName = productName;
        this.expDate = expDate;
    }

    public Medicine(String cis,
                    String productName,
                    long expDate,
                    String prodFormNormName,
                    String prodDNormName,
                    String phKinetics,
                    Technical technical) {
        this.cis = cis;
        this.productName = productName;
        this.expDate = expDate;
        this.prodFormNormName = prodFormNormName;
        this.prodDNormName = prodDNormName;
        this.phKinetics = phKinetics;
        this.technical = technical;
    }

    @Ignore
    public Medicine(long id,
                    String cis,
                    String productName,
                    long expDate,
                    String prodFormNormName,
                    String prodDNormName,
                    double prodAmount,
                    String phKinetics,
                    String comment,
                    Technical technical) {
        this.id = id;
        this.cis = cis;
        this.productName = productName;
        this.expDate = expDate;
        this.prodFormNormName = prodFormNormName;
        this.prodDNormName = prodDNormName;
        this.prodAmount = prodAmount;
        this.phKinetics = phKinetics;
        this.comment = comment;
        this.technical = technical;
    }
}
