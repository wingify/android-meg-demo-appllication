# Android MEG Demo Application

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

## Official SDK Documentation

Refer [Official VWO Documentation](https://developers.vwo.com/reference/android-introduction)

## Assumptions

* This is a code-only workaround i.e. not controlled via VWO Application
* A campaign can be a part of only one group
* A group must have at least 2 campaigns

## Usage

**Group Schema**

```json
{ "id": 11, "name": "Optional name", "campaignList": [<list-of-campaign-keys>] }
```

**Creating Groups**

```java
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
group2.addCampaign("23"); // will not be added to group 2 because it already belongs to group 1
group2.addCampaign("25");
group2.addCampaign("26");

groups.put(group1.getName(), group1);
groups.put(group2.getName(), group2);

```

**There are 2 ways of getting the campaign**

1. Passing campaign-key

    ```java
    HashMap<String, String> args = new HashMap<>();
    args.put(MutuallyExclusiveGroups.ID_CAMPAIGN, "24");

    String campaign = getCampaign(USER_ID, args);
    ```

    `campaign` will be `null`, if `campaignKey` passed is not the same as the winning campaign. Otherwise, the same `campaignkey` will be returned.


2. Passingg group-id

    ```java
    HashMap<String, String> args = new HashMap<>();
    args.put(MutuallyExclusiveGroups.ID_GROUP, "2");

    String campaign = getCampaign(USER_ID, args);
    ```

    Campaign will be one of the campaigns from the group i.e. the winning campaign.


> **Note**: When both campaign-key and group-id are passed, evaluation will be based on groupId only
>
> ```java
>HashMap<String, String> args = new HashMap<>();
>args.put(MutuallyExclusiveGroups.ID_CAMPAIGN, "24");
>args.put(MutuallyExclusiveGroups.ID_GROUP, "2");
>
>String campaign = getCampaign(USER_ID, args);
>```

## API Flow

**getCampaign API Flow when campaign-key is passed**

* Checks if a campaign is a part of any group
* If not
  * returns that campaign
* If yes:
  * Generates a random number/murmurhash corresponding to the User ID
  * Normalises the UUID to be within 1-100
  * Assigns equal weights to campaigns in a group
  * Checks where the normalied value lies within a normally weighted group and picks that campaign(winning campaign)
  * If the winning campaign matches with the passed campaign, return that campaign, otherwise null

**getCampaign API Flow when group-id is passed**

* Checks if the group exists
* If no:
  * return null
* If yes:
  * Generates a random number/murmurhash corresponding to the User ID
  * Normalises the UUID to be within 1-100
  * Assigns equal weights to campaigns in a group
  * Checks where the normalied value lies within a normally weighted group and picks that campaign(winning campaign)
  * Returns the winning campaign

## Example Usage

```java
VWOConfig config = new VWOConfig.Builder()
        .userID(USER_ID)
        .build();

VWO.with(this, key)
        .config(config)
        .launch(new VWOStatusListener() {
            @Override
            public void onVWOLoaded() {
                log("VWO initialization success -> afterInitSuccess()");

                HashMap<String, String> args = new HashMap<>();

                // args.put(MutuallyExclusiveGroups.ID_CAMPAIGN, "24");
                args.put(MutuallyExclusiveGroups.ID_GROUP, "2");

                String mutuallyExclusiveCampaign = getCampaign(USER_ID, args);
            }

            @Override
            public void onVWOLoadFailure(String reason) {
                showToast("VWO init failed.");
            }
        });
```

## Authors

* Nabin Niroula - [xyznaveen](https://github.com/xyznaveen)

## Contributing

Please go through our [contributing guidelines](https://github.com/wingify/android-meg-demo-application/blob/master/CONTRIBUTING.md)

## Code of Conduct

[Code of Conduct](https://github.com/wingify/android-meg-demo-application/blob/master/CODE_OF_CONDUCT.md)

## License

[Apache License, Version 2.0](https://github.com/wingify/android-meg-demo-application/blob/master/LICENSE)

Copyright 2022 Wingify Software Pvt. Ltd.
