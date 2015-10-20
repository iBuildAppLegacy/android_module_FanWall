/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.FanWallPlugin.data;

import android.text.TextUtils;
import com.appbuilder.sdk.android.authorization.entities.User;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Entity class that represents message of the fan wall.
 */
public class FanWallMessage implements Serializable {

    private final String DATE_PATTERN = "d MMM yyyy HH:mm:ss Z";
    private long id = 0;
    private long parentId = 0;
    private long replyId = 0;
    private long likeCount;
    private long sharingCount;
    private boolean likedByMe;
    private String author = "";
    private String text = "";
    private String imageUrl = "";
    private String imageCachePath = "";
    private String userAvatarUrl = "";
    private String userAvatarCache = "";
    private User.ACCOUNT_TYPES accountType = User.ACCOUNT_TYPES.IBUILDAPP;
    private String accountId = "";
    private int totalComments = 0;
    private float latitude = 1000;
    private float longitude = 1000;
    private Date date = null;
    private ArrayList<FanWallMessage> comments = new ArrayList<FanWallMessage>();

    /**
     * Constructs new fan wall message.
     */
    public FanWallMessage() {
    }

    /**
     * Constructs new fan wall message with given parameters.
     *
     * @param author author's name
     * @param text   message text
     */
    public FanWallMessage(String author, String text) {
        this.author = author;
        this.text = text;
    }

    /**
     * Constructs new fan wall message with given parameters.
     *
     * @param author author's name
     * @param text   message text
     * @param date   message date string with 'd MMM yyyy HH:mm:ss Z' pattern
     * @throws ParseException
     */
    public FanWallMessage(String author, String text, String date) throws ParseException {
        this.author = author;
        this.text = text;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        try {
            this.date = sdf.parse(date);
        } catch (IllegalArgumentException iAEx) {
            throw new ParseException(date, 0);
        }
    }

    /**
     * Constructs new fan wall message with given parameters.
     *
     * @param author author's name
     * @param text   message text
     * @param date   message date
     */
    public FanWallMessage(String author, String text, Date date) {
        this.author = author;
        this.text = text;
        this.date = date;
    }

    public boolean isLikedByMe() {
        return likedByMe;
    }

    public void setLikedByMe(boolean likedByMe) {
        this.likedByMe = likedByMe;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getSharingCount() {
        return sharingCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public void setSharingCount(long sharingCount) {
        this.sharingCount = sharingCount;
    }

    /**
     * Returns the author's name.
     *
     * @return the name string
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author's name.
     *
     * @param author the name to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the message text.
     *
     * @return the message text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the message text.
     *
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the attached image URL.
     *
     * @return the image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the attached image URL.
     *
     * @param imageUrl the image URL to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Checks if this message has attached image.
     *
     * @return true if this message has image, false otherwise
     */
    public boolean hasImage() {
        return (imageUrl.length() > 0);
    }

    /**
     * Returns the cache path of attached image.
     *
     * @return the cache path string
     */
    public String getImageCachePath() {
        return imageCachePath;
    }

    /**
     * Sets the cache path of attached image.
     *
     * @param imageCache the cache path string to set
     */
    public void setImageCachePath(String imageCache) {
        this.imageCachePath = imageCache;
    }

    /**
     * Checks if attached image already downloaded.
     *
     * @return true if image already downloaded, false otherwise
     */
    public boolean hasImageCache() {
        return (imageUrl.length() > 0);
    }

    /**
     * Returns the count of commetnts that was made to this message.
     *
     * @return the count of comments
     */
    public int getTotalComments() {
        return totalComments;
    }

    /**
     * Sets the count of commetnts that was made to this message.
     *
     * @param totalComments the count of commetns to set
     */
    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    /**
     * Returns the latitude of location in which message was made on.
     *
     * @return the latitude
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude of location which message was made on.
     *
     * @return the longitude
     */
    public float getLongitude() {
        return longitude;
    }

    /**
     * Sets the location point which message was made on.
     *
     * @param latitude  the point latitude
     * @param longitude the point longitude
     */
    public void setPoint(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the message date.
     *
     * @return the message date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the message date.
     *
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Sets the message date
     *
     * @param date the date string with 'd MMM yyyy HH:mm:ss Z' pattern
     * @throws ParseException
     */
    public void setDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        try {
            this.date = sdf.parse(date);
        } catch (IllegalArgumentException iAEx) {
            throw new ParseException(date, 0);
        }
    }

    /**
     * Returns the comment list that was made on this message.
     *
     * @return the comment list
     */
    public ArrayList<FanWallMessage> getComments() {
        return comments;
    }

    /**
     * Returns the comment list that was made on this message.
     *
     * @param comments the comment list to set
     */
    public void setComments(ArrayList<FanWallMessage> comments) {
        this.comments = comments;
    }

    /**
     * Adds the comment that was made on this message.
     *
     * @param comment the comment to add
     */
    public void addComment(FanWallMessage comment) {
        this.comments.add(comment);
        totalComments++;
    }

    /**
     * Adds the list of comments that was made on this message.
     *
     * @param comments the list of comments
     */
    public void addComments(ArrayList<FanWallMessage> comments) {
        this.comments.addAll(comments);
        totalComments = totalComments + comments.size();
    }

    /**
     * Returns the user avatar image URL.
     *
     * @return the image URL
     */
    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    /**
     * Sets the author's avatar image URL.
     *
     * @param userAvatarUrl the image URL to set
     */
    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    /**
     * Checks if the message author has avatar.
     *
     * @return true if this user has avatar
     */
    public boolean hasAvatar() {
        return !TextUtils.isEmpty(userAvatarUrl);
    }

    /**
     * Returns the author's avatar image cache path.
     *
     * @return the image cache path
     */
    public String getUserAvatarCache() {
        return userAvatarCache;
    }

    /**
     * Sets the author's avatar image cache path.
     *
     * @param userAvatarCache the image cache path to set
     */
    public void setUserAvatarCache(String userAvatarCache) {
        this.userAvatarCache = userAvatarCache;
    }

    /**
     * Checks if author's avatar image already downloaded.
     *
     * @return true if image already downloaded, false otherwise
     */
    public boolean hasUserAvatarCache() {
        return (userAvatarCache.length() > 0);
    }

    /**
     * Returns the message ID.
     *
     * @return the message ID
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the message ID.
     *
     * @param id the ID to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the message ID which this message is comment on.
     *
     * @return the message ID
     */
    public long getParentId() {
        return parentId;
    }

    /**
     * Sets the message ID which this message is comment on.
     *
     * @param parentId the message ID to set
     */
    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    /**
     * Returns the wall message ID which this message is comment on.
     *
     * @return the wall message ID
     */
    public long getReplyId() {
        return replyId;
    }

    /**
     * Sets the wall message ID which this message is comment on.
     *
     * @param replyId the wall message to set
     */
    public void setReplyId(long replyId) {
        this.replyId = replyId;
    }

    /**
     * Returns the author's accoutn type
     *
     * @return the account type
     */
    public User.ACCOUNT_TYPES getAccountType() {
        return accountType;
    }

    /**
     * Sets the author's account type.
     *
     * @param accountType the account type to set
     */
    public void setAccountType(String accountType) {
        if (accountType.equalsIgnoreCase("facebook")) {
            this.accountType = User.ACCOUNT_TYPES.FACEBOOK;
        } else if (accountType.equalsIgnoreCase("twitter")) {
            this.accountType = User.ACCOUNT_TYPES.TWITTER;
        } else {
            this.accountType = User.ACCOUNT_TYPES.IBUILDAPP;
        }
    }

    /**
     * Returns the author's account ID.
     *
     * @return the account ID
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Sets the author's account ID.
     *
     * @param accountId the account ID to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
