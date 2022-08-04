package com.vwo.vwomeg;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vwo.mobile.VWO;
import com.vwo.mobile.VWOConfig;
import com.vwo.mobile.events.VWOStatusListener;
import com.vwo.vwomeg.hash.MurmurHash;
import com.vwo.vwomeg.meg.Group;
import com.vwo.vwomeg.meg.MutuallyExclusiveGroups;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "VWO_MEG";

    private static final String VWO_KEY = "20e1d184e5f84cbbd265f09a5c861a8a-615626";

    // ------------------------------------------------
    // normalized value     ->      userId
    // ------------------------------------------------
    //  7                   ->      9c3832ad-15f9-420a-93cd-a7f2cde0f7bc
    // 18                   ->      4af04968-b1d3-405c-9142-04ea711defaf
    // 27                   ->      user1@example.com
    // 39                   ->      e57e8bd1-fb5f-478d-80d2-5127eb5d79f7
    // 49                   ->      mark@facebook.com
    // 53                   ->      05f93be0-a8e3-4b42-8698-75e5ae2c39f7
    // 69                   ->      itis@whatit.is
    // 77                   ->      the.amzing@spider.man
    // 85                   ->      ifyouknow@whati.mean
    // 98                   ->      a0aed829-a96f-45f0-b987-f1a249f87020
    private static final String USER_ID = "9c3832ad-15f9-420a-93cd-a7f2cde0f7bc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchVwo(VWO_KEY);
    }

    private void launchVwo(String key) {

        HashMap<String, String> customVariables = new HashMap<>();

        VWOConfig config = new VWOConfig.Builder()
                .setCustomVariables(customVariables)
                .setOptOut(false)
                .userID(USER_ID)
                .build();

        VWO.with(this, key)
                .config(config)
                .launch(new VWOStatusListener() {
                    @Override
                    public void onVWOLoaded() {
                        log("VWO initialization success -> afterInitSuccess()");

                        HashMap<String, String> args = new HashMap<>();
                        args.put(MutuallyExclusiveGroups.ID_CAMPAIGN, "23");
                        // args.put(MutuallyExclusiveGroups.ID_GROUP, "2");

                        String yourCampaign = getCampaign(USER_ID, args);
                    }

                    @Override
                    public void onVWOLoadFailure(String reason) {
                        showToast("VWO init failed.");
                    }
                });
    }

    private String getCampaign(String userId, HashMap<String, String> args) {
        // STEP 1: Create object by passing user id
        MutuallyExclusiveGroups meg = new MutuallyExclusiveGroups(userId);

        // STEP 2: Add the groups that you have created
        meg.addGroups(createGroupCampaignMapping());

        // STEP 3: Get the campaign name based on the logic
        //         https://confluence.wingify.com/pages/viewpage.action?spaceKey=VWOENG&title=Mobile+Testing+-+Mutually+Exclusive+Groups
        return meg.getCampaign(args);
    }

    /**
     * Just create a group and it's campaigns in a Key Value pair format.
     *
     * @return the key value pair for the campaigns and groups
     */
    private HashMap<String, Group> createGroupCampaignMapping() {

        HashMap<String, Group> groups = new HashMap<>();

        Group group1 = new Group();
        group1.setId(1);
        group1.setName("Group 1");
        group1.addCampaign("22");
        group1.addCampaign("23");
        group1.addCampaign("24");

        Group group2 = new Group();
        group2.setId(2);
        group2.setName("Group 2");
        group2.addCampaign("23"); // will not be added to group 2 because it already belongs to group 1; check logs with respective tags
        group2.addCampaign("25");
        group2.addCampaign("26");

        groups.put(group1.getName(), group1);
        groups.put(group2.getName(), group2);

        log("created " + groups.size() + " groups ...");
        log("[ " + group1.getName() + " ] has " + Objects.requireNonNull(groups.get(group1.getName())).getCampaignSize() + " items.");
        log("[ " + group1.getName() + " ] weight for each item " + group1.getWeight());
        log("[ " + group2.getName() + " ] has " + Objects.requireNonNull(groups.get(group2.getName())).getCampaignSize() + " items.");
        log("[ " + group2.getName() + " ] weight for each item " + group2.getWeight());

        return groups;
    }

    private void showToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void log(String message) {
        MutuallyExclusiveGroups.log(message);
    }

}