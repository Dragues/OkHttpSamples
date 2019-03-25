package com.example.ilya.mytestnetworkapp;


import com.google.gson.annotations.SerializedName;

/**
 *
 * <quote>
 * <quoteText>Краткость — сестра таланта</quoteText>
 * <quoteAuthor></quoteAuthor>
 * <senderName>имя или ник кто прислал цитату</senderName>
 * <senderLink>email или адрес сайта того кто прислал</senderLink>
 * </quote>
 *
  */

public class Quote {

    @SerializedName("quoteText")
    private String quoteText;

    @SerializedName("quoteAuthor")
    private String quoteAuthor;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("senderLink")
    private String senderLink;


}
