package com.vwo.vwomeg.meg;

import android.util.Log;

import com.vwo.vwomeg.hash.MurmurHash;

import java.util.HashMap;

public class MutuallyExclusiveGroups {

    private static final boolean IS_LOGS_SHOWN = true;

    public static final String ID_GROUP = "groupId";

    public static final String ID_CAMPAIGN = "campaignKey";

    private static final String TAG = MutuallyExclusiveGroups.class.getSimpleName();

    private final HashMap<String, Group> CAMPAIGN_GROUPS = new HashMap<>();

    private final String userId;

    private final HashMap<Integer, String> USER_CAMPAIGN = new HashMap<>();

    public MutuallyExclusiveGroups(String userId) {
        this.userId = userId;
    }

    public void addGroups(HashMap<String, Group> groupHashMap) {
        CAMPAIGN_GROUPS.clear();
        CAMPAIGN_GROUPS.putAll(groupHashMap);
    }

    public String getCampaign(HashMap<String, String> args) {
        return calculateTheWinnerCampaign(args);
    }

    /*private void saveUserCampaignInSharedPreferences(String userId, String campaign) {
        if (userId == null || campaign == null) return;
        try {
            @SuppressLint("PrivateApi") Application application
                    = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);

            if (application == null) return;

            SharedPreferences sp = application.getSharedPreferences("__vwo_workaround.client", Context.MODE_PRIVATE);
            sp.edit().putString("userId", userId).apply();
            sp.edit().putString("campaign", campaign).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private String calculateTheWinnerCampaign(HashMap<String, String> args) {

        if (args == null) {
            log("the [ args ] cannot be null.");
            return null;
        }

        String groupId = args.get(ID_GROUP);
        String campaignKey = args.get(ID_CAMPAIGN);

        if (groupId == null && campaignKey == null) {

            // there must be at least one type of id
            // either GROUP or CAMPAIGN
            log("the [groupId] and [campaignKey] ; both are null.");
            return null;
        }

        String campaign;

        boolean groupIdIsNotPresentInArgs = (groupId == null);
        if (groupIdIsNotPresentInArgs) {

            log(ID_GROUP + " was not found in the mapping so just picking the specific campaign [ " + campaignKey + " ]");
            // if there is no sign of group we can simply use the campaign matching logic
            campaign = getCampaignFromCampaignId(userId, campaignKey);
            log("selected campaign -> [ " + campaign + " ]");
            return campaign;
        }

        log("because there was [groupId] in the [args] we are going to prioritize it and get a campaign from that group");
        String groupName = getGroupNameFromGroupId(Integer.parseInt(groupId));
        campaign = getCampaignFromSpecificGroup(groupName);
        log("selected campaign from [ " + groupName + " ] -> [ " + campaign + " ]");

        return campaign;
    }

    private String getCampaignFromSpecificGroup(String groupName) {

        if (groupName == null) {
            // this should never happen unless the id of the group that doesn't exist is passed
            return null;
        }

        int murmurHash = getMurMurHash(userId);

        // If the campaign-user mapping is present in the App storage, get the decision from there. Otherwise, go to the next step
        if (USER_CAMPAIGN.containsKey(murmurHash)) return USER_CAMPAIGN.get(murmurHash);

        int normalizedValue = getNormalizedValue(murmurHash);
        log("normalized value for user -> " + userId + " <- is || " + normalizedValue + " ||");

        Group interestedGroup = CAMPAIGN_GROUPS.get(groupName);
        if (interestedGroup == null) return null;

        return interestedGroup.getCampaignForRespectiveWeight(normalizedValue);
    }

    private String getGroupNameFromGroupId(int groupId) {
        for (String key :
                CAMPAIGN_GROUPS.keySet()) {

            Group group = CAMPAIGN_GROUPS.get(key);

            if (group == null) return null;

            if (groupId == group.getId()) {
                // we found the group we have been searching for
                return key;
            }
        }
        return null;
    }

    private String getCampaignFromCampaignId(String userId, String campaign) {

        String campaignFoundInGroup = getCampaignIfPresent(campaign);
        if (campaignFoundInGroup == null) {

            log("the key [ " + campaign + " ] is not present in any of the groups.");
            return campaign;
        } else {

            log("found campaign [ " + campaign + " ] in group " + campaignFoundInGroup);
        }

        // Generate a random number/murmurhash corresponding to the User ID
        int murmurHash = getMurMurHash(userId);

        // If the campaign-user mapping is present in the App storage, get the decision from there. Otherwise, go to the next step
        if (USER_CAMPAIGN.containsKey(murmurHash)) return USER_CAMPAIGN.get(murmurHash);

        int normalizedValue = getNormalizedValue(murmurHash);
        log("normalized value for user -> " + userId + " <- is || " + normalizedValue + " ||");

        // this group has our campaign
        Group interestedGroup = CAMPAIGN_GROUPS.get(campaignFoundInGroup);

        if (interestedGroup == null)
            return null; // basic null check because HashMap is being used

        String finalCampaign = interestedGroup.getCampaignForRespectiveWeight(normalizedValue);
        if (campaign.equals(finalCampaign)) {
            return finalCampaign;
        } else {
            log("passed campaign : " + campaign + " does not match calculated campaign " + finalCampaign);
        }

        return null;
    }

    private String getCampaignIfPresent(String campaignKey) {
        for (String key :
                CAMPAIGN_GROUPS.keySet()) {

            Group group = CAMPAIGN_GROUPS.get(key);

            if (group == null) return null;

            String foundCampaign = group.getOnlyIfPresent(campaignKey);
            if (foundCampaign != null) {

                // we should return name of the group
                // the reason being we need to use the weightage of the campaigns later on
                return key;
            }
        }
        return null;
    }

    private int getNormalizedValue(int murmurHash) {
        int max = 100; // our normalized data ranges from { 0 to 100 }
        double ratio = murmurHash / Math.pow(2, 31);
        double multipliedValue = (max * ratio) + 1;
        int value = Math.abs((int) Math.floor(multipliedValue));
        log("the normalized value for " + murmurHash + " is " + value);
        return value;
    }

    private int getMurMurHash(String userId) {
        int hash = Math.abs(MurmurHash.hash32(userId));
        log("murmurhash for [ " + userId + " ] -> [ " + hash + " ]");
        return hash;
    }

    public static void log(String message) {
        if (IS_LOGS_SHOWN) {
            Log.i(TAG, message);
        }
    }

}
