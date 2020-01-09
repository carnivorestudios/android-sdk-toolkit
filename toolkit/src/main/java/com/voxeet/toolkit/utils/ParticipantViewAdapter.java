package com.voxeet.toolkit.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.MediaStreamType;
import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.models.v1.ConferenceParticipantStatus;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.toolkit.R;
import com.voxeet.toolkit.views.internal.rounded.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ParticipantViewAdapter extends RecyclerView.Adapter<ParticipantViewAdapter.ViewHolder> {

    private final String TAG = ParticipantViewAdapter.class.getSimpleName();

    private boolean namesEnabled = true;

    private List<Participant> users;

    private Context context;

    private int avatarSize;

    private int lastPosition = -1;

    private String selectedUserId = null;

    private IParticipantViewListener listener;

    private int selectedUserColor;

    private String mRequestUserIdChanged;

    private ParticipantViewAdapter() {

    }

    /**
     * Instantiates a new Participant view adapter.
     *
     * @param context the context
     */
    public ParticipantViewAdapter(Context context) {
        this();
        this.selectedUserColor = context.getResources().getColor(R.color.blue);

        this.context = context;

        this.users = new ArrayList<>();

        this.namesEnabled = true;

        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.meeting_list_avatar_double);
    }

    public void updateUsers() {
        filter();
        sort();

        notifyDataSetChanged();
    }

    /**
     * Set the corresponding users
     *
     * @param users the list of user to populate the adapter
     */
    public void setUsers(List<Participant> users) {
        for (Participant user : users) {
            if (!this.users.contains(user))
                this.users.add(user);
        }

        List<Participant> to_remove = new ArrayList<>();
        for (Participant user : this.users) {
            if (!users.contains(user)) to_remove.add(user);
        }
        this.users.removeAll(to_remove);

        filter();
        sort();
    }

    private void filter() {
        /*
        List<Participant> temp_users = new ArrayList<>();

        for (Participant user : users) {
            if (!ConferenceParticipantStatus.LEFT.equals(user.getStatus())) temp_users.add(user);
        }

        users = temp_users;
        */
    }

    private boolean is(Participant p, ConferenceParticipantStatus s) {
        return null != p && s.equals(p.getStatus());
    }

    private void sort() {
        if (null != users) {
            ArrayList<Participant> tmp = new ArrayList<>();
            ArrayList<Participant> air = new ArrayList<>();
            ArrayList<Participant> inv = new ArrayList<>();
            ArrayList<Participant> left = new ArrayList<>();
            ArrayList<Participant> other = new ArrayList<>();

            for (Participant participant : users) {
                if (participant.isLocallyActive()) {
                    air.add(participant);
                } else if (is(participant, ConferenceParticipantStatus.RESERVED)) {
                    inv.add(participant);
                } else if (is(participant, ConferenceParticipantStatus.LEFT)) {
                    left.add(participant);
                } else {
                    other.add(participant);
                }
            }
            tmp.addAll(air);
            tmp.addAll(inv);
            tmp.addAll(left);
            tmp.addAll(other);
            users = tmp;
        }
    }

    /**
     * Sets the color when a conference user has been selected.
     *
     * @param color the color
     */
    public void setSelectedUserColor(int color) {
        selectedUserColor = color;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_participant_view_cell, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Participant user = getItem(position);

        boolean on_air = user.isLocallyActive();

        if (null != user.getInfo()) {
            holder.name.setText(user.getInfo().getName());
        }
        holder.name.setVisibility(namesEnabled ? View.VISIBLE : View.GONE);

        loadViaPicasso(user, holder.avatar);

        if (on_air) {
            holder.itemView.setAlpha(1f);
            holder.avatar.setAlpha(1.0f);
        } else {
            holder.itemView.setAlpha(0.5f);
            holder.avatar.setAlpha(0.4f);
        }

        if (equalsToUser(selectedUserId, user)) {
            holder.name.setTypeface(Typeface.DEFAULT_BOLD);
            holder.name.setTextColor(context.getResources().getColor(R.color.white));

            holder.overlay.setVisibility(View.VISIBLE);
            holder.overlay.setBackgroundColor(selectedUserColor);
        } else {
            holder.name.setTypeface(Typeface.DEFAULT);
            holder.name.setTextColor(context.getResources().getColor(R.color.grey999));

            holder.overlay.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (equalsToUser(selectedUserId, user)) {
                    selectedUserId = null;

                    if (listener != null)
                        listener.onParticipantUnselected(user);
                    notifyDataSetChanged();
                }
                return true;
            }
        });

        if (null != mRequestUserIdChanged && mRequestUserIdChanged.equals(user.getId())) {
            MediaStream stream = getMediaStream(mRequestUserIdChanged);

            loadStreamOnto(mRequestUserIdChanged, holder);

            if (listener != null)
                listener.onParticipantSelected(user, stream);

            //prevent any modification until next event
            mRequestUserIdChanged = null;
        } else {
            String userId = user.getId();

            loadStreamOnto(userId, holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!on_air) {
                    Log.d(TAG, "onClick: click on an invalid user, we can't select him");
                    return;
                }

                String userId = user.getId();
                //toggle media screen call next stream

                loadStreamOnto(userId, holder);


                MediaStream stream = getMediaStream(userId);

                if (null != user.getId()) {
                    Log.d(TAG, "onClick: selecting the user " + user.getId());
                    if (null == selectedUserId || !equalsToUser(selectedUserId, user)) {
                        selectedUserId = user.getId();

                        if (listener != null)
                            listener.onParticipantSelected(user, stream);
                    } else {
                        selectedUserId = null; //deselecting

                        if (listener != null)
                            listener.onParticipantUnselected(user);
                    }

                    notifyDataSetChanged();
                }
            }
        });

        setAnimation(holder.itemView, position);
    }

    private boolean equalsToUser(String selectedUserId, Participant user) {
        return null != selectedUserId && null != user && selectedUserId.equals(user.getId());
    }

    private void loadStreamOnto(@Nullable String userId, @NonNull ViewHolder holder) {
        MediaStream normalStream = getMediaStream(userId);

        Log.d("VideoView", "loadStreamOnto: having stream for user " + userId + " := " + (null != normalStream ? normalStream.peerId() + " " + normalStream.videoTracks().size() : "") + " " + normalStream + " " + holder);

        if (null != normalStream && normalStream.videoTracks().size() > 0 && userId.equalsIgnoreCase(normalStream.peerId())) {
            holder.videoView.attach(userId, normalStream);
            holder.videoView.setVisibility(View.VISIBLE);
            holder.avatar.setVisibility(View.GONE);
        } else {
            holder.videoView.unAttach();
            holder.videoView.setVisibility(View.GONE);
            holder.avatar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Animation when a new participant is joining the conference.
     *
     * @param viewToAnimate the valid view which must be animated
     * @param position      the position in the list
     */
    private void setAnimation(@NonNull View viewToAnimate, int position) {
        if (position > lastPosition) { // If the bound view wasn't previously displayed on screen, it's animated
            AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
            animation.setDuration(500);
            animation.setFillAfter(true);
            viewToAnimate.startAnimation(animation);

            lastPosition = position;
        }
    }

    private Participant getItem(int position) {
        return users.get(position);
    }

    /**
     * Displays user's avatar in the specified imageView.
     *
     * @param conferenceUser a valid user to bind into picasso
     * @param imageView      the landing image view
     */

    private void loadViaPicasso(@NonNull Participant conferenceUser, ImageView imageView) {
//        R.drawable.default_avatar
        try {
            String url = conferenceUser.getUserInfo().getAvatarUrl();
            String avatarName = "";
            if (null != conferenceUser && null != conferenceUser.getUserInfo()) {
                avatarName = conferenceUser.getUserInfo().getName();
            }
            ColorGenerator generator = ColorGenerator.MATERIAL;
            if (avatarName.length() >= 2) {
                avatarName = avatarName.substring(0, 2);
            } else if (avatarName.length() >= 1) {
                avatarName = avatarName.substring(0, 1);
            }
            int color2 = generator.getColor(avatarName);
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(imageView.getWidth())  // width in px
                    .height(imageView.getHeight()) // height in px
                    .toUpperCase()
                    .bold()
                    .endConfig()
                    .buildRect(avatarName, color2);
            if (!TextUtils.isEmpty(url)) {
//                imageView.setImageDrawable(drawable);
                Picasso.get()
                        .load(url)
                        .noFade()
                        .resize(avatarSize, avatarSize)
                        .placeholder(drawable)
                        .error(drawable)
                        .into(imageView);
            } else {
//                Picasso.get()
//                        .load(R.drawable.default_avatar)
//                        .into(imageView);
                imageView.setImageDrawable(drawable);
            }
        } catch (Exception e) {
            Log.e(TAG, "error " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * Sets participant listener.
     *
     * @param listener the listener
     */
    public void setParticipantListener(IParticipantViewListener listener) {
        this.listener = listener;
    }

    /**
     * @param user
     */
    public void onMediaStreamUpdated(@Nullable Participant user) {
        //removed unecessary change of current selected it
        /*
        MediaStream stream = user.streamsHandler().getFirst(MediaStreamType.Camera);
        if (null != stream && stream.videoTracks().size() > 0) {
            mRequestUserIdChanged = user.getId();
        }
        */

        notifyDataSetChanged();
    }

    /**
     * Clear participants.
     */
    public void clearParticipants() {
        this.users.clear();
    }

    /**
     * Sets names enabled.
     *
     * @param enabled the enabled
     */
    public void setNamesEnabled(boolean enabled) {
        namesEnabled = enabled;
    }

    public String getSelectedUserId() {
        return mRequestUserIdChanged;
    }

    /**
     * The type View holder.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        private VideoView videoView;

        private TextView name;

        private RoundedImageView avatar;

        private ImageView overlay;

        /**
         * Instantiates a new View holder.
         *
         * @param view the view
         */
        ViewHolder(@NonNull View view) {
            super(view);

            videoView = (VideoView) view.findViewById(R.id.participant_video_view);

            name = (TextView) view.findViewById(R.id.name);

            overlay = (ImageView) view.findViewById(R.id.overlay_avatar);

            avatar = (RoundedImageView) view.findViewById(R.id.avatar);
        }
    }

    @Nullable
    private MediaStream getMediaStream(@Nullable String userId) {
        Participant user = VoxeetSdk.conference().findParticipantById(userId);
        return null != user ? user.streamsHandler().getFirst(MediaStreamType.Camera) : null;
    }
}
