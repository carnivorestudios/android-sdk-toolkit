package com.voxeet.toolkit.implementation;

import com.voxeet.android.media.MediaStream;
import com.voxeet.sdk.models.User;

import java.util.List;
import java.util.Map;


/**
 * Created by kevinleperf on 15/01/2018.
 */

public interface IVoxeetView {
    /**
     * On conference joined.
     *
     * @param conferenceId the conference id
     */
    void onConferenceJoined(String conferenceId);

    /**
     * On conference updated.
     *
     * @param conferenceId the conference id
     */
    void onConferenceUpdated(List<User> conferenceId);

    /**
     * On conference creation.
     *
     * @param conferenceId the conference id
     */
    void onConferenceCreation(String conferenceId);

    /**
     * On conference user joined.
     *
     * @param conferenceUser the conference user
     */
    void onConferenceUserJoined(User conferenceUser);

    /**
     * On conference for user joined
     */
    void onConferenceFromNoOneToOneUser();

    /**
     * On conference no more users.
     */
    void onConferenceNoMoreUser();

    /**
     * On conference user updated.
     *
     * @param conferenceUser the conference user
     */
    void onConferenceUserUpdated(User conferenceUser);

    /**
     * On conference user left.
     *
     * @param conferenceUser the conference user
     */
    void onConferenceUserLeft(User conferenceUser);

    /**
     * An user declined the call
     *
     * @param userId the declined-user id
     */
    void onConferenceUserDeclined(String userId);

    /**
     * On recording status updated.
     *
     * @param recording the recording
     */
    void onRecordingStatusUpdated(boolean recording);

    /**
     * On media stream updated.
     *
     * @param userId       the user id
     * @param mediaStreams
     */
    void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams);

    /**
     * @param conferenceUsers the new list of users
     */
    void onConferenceUsersListUpdate(List<User> conferenceUsers);

    /**
     * @param mediaStreams the new list of media streams
     */
    void onMediaStreamsListUpdated(Map<String, MediaStream> mediaStreams);

    /**
     * @param mediaStreams the new list of mediaStreams
     */
    void onMediaStreamsUpdated(Map<String, MediaStream> mediaStreams);

    /**
     * On conference mute from this user.
     */
    void onConferenceMute(Boolean isMuted);

    /**
     * On conference turn on video from this user.
     */
    void onConferenceVideo(Boolean isVideoEnabled);

    /**
     * On conference call end from this user.
     */
    void onConferenceCallEnded();

    /**
     * On conference minimized from this user.
     */
    void onConferenceMinimized();

    /**
     * On conference Speaker On from this user.
     */
    void onConferenceSpeakerOn(Boolean isSpeakerOn);

    /**
     * @param screenShareMediaStreams the new list of screen share media streams
     */
    void onScreenShareMediaStreamUpdated(Map<String, MediaStream> screenShareMediaStreams);

    /**
     * On conference leaving from this user.
     */
    void onConferenceLeaving();

    /**
     * On conference destroyed.
     */
    void onConferenceDestroyed();

    /**
     * On conference left.
     */

    void onConferenceLeft();

    /**
     * View resumed
     */
    void onResume();

    /**
     * View stopped
     * typically when conference stopped
     */
    void onStop();

    /**
     * View destroyed
     */
    void onDestroy();

    /**
     * After init
     */
    void onInit();
}
