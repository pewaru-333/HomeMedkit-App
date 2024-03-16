package ru.application.homemedkit.databaseController;

import static ru.application.homemedkit.helpers.ConstantsHelper.BLANK;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicines")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    public long id = 0;
    public String cis = BLANK;
    public String productName = BLANK;
    public long expDate = -1L;
    public String prodFormNormName = BLANK;
    public String prodDNormName = BLANK;
    public double prodAmount = -1.0;
    public String phKinetics = BLANK;
    public String comment = BLANK;
    @Embedded
    public Technical technical = new Technical();

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
