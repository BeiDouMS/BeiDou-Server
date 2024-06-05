package org.gms.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gms.config.YamlConfig;
import org.gms.tools.BCrypt;
import org.gms.tools.HexTool;

import java.io.Serial;

/**
 *  实体类。
 *
 * @author sleep
 * @since 2024-05-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("accounts")
public class AccountsDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String name;

    @JsonIgnore
    private String password;

    private String pin;

    private String pic;

    private Integer loggedin;

    private Timestamp lastlogin;

    private Timestamp createdat;

    private Date birthday;

    private Boolean banned;

    private String banreason;

    private String macs;

    @Column("nxCredit")
    private Integer nxCredit;

    @Column("maplePoint")
    private Integer maplePoint;

    @Column("nxPrepaid")
    private Integer nxPrepaid;

    private Integer characterslots;

    private Integer gender;

    private Timestamp tempban;

    private Integer greason;

    private Boolean tos;

    private String sitelogged;

    private Integer webadmin;

    private String nick;

    private Integer mute;

    private String email;

    private String ip;

    private Integer rewardpoints;

    private Integer votepoints;

    private String hwid;

    private Integer language;

    public void encryptPassword() throws NoSuchAlgorithmException {
        this.password = YamlConfig.config.server.BCRYPT_MIGRATION ? BCrypt.hashpw(this.password, BCrypt.gensalt(12)) : hashPwSHA512(this.password);
    }

    public boolean checkPassword(String pwd) {
        String passHash = this.password;
        if (passHash.charAt(0) == '$' && passHash.charAt(1) == '2' && BCrypt.checkpw(pwd, passHash)) {
            return true;
        } else return pwd.equals(passHash) || checkHash(passHash, "SHA-1", pwd) || checkHash(passHash, "SHA-512", pwd);
    }

    private static String hashPwSHA512(String pwd) throws NoSuchAlgorithmException {
        MessageDigest digester = MessageDigest.getInstance("SHA-512");
        digester.update(pwd.getBytes(StandardCharsets.UTF_8), 0, pwd.length());
        return HexTool.toHexString(digester.digest()).replace(" ", "").toLowerCase();
    }

    private static boolean checkHash(String hash, String type, String password) {
        try {
            MessageDigest digester = MessageDigest.getInstance(type);
            digester.update(password.getBytes(StandardCharsets.UTF_8), 0, password.length());
            return HexTool.toHexString(digester.digest()).replace(" ", "").toLowerCase().equals(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Encoding the string failed", e);
        }
    }
}
