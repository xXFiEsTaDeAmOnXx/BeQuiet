package com.example.bequiet.model.database;

import android.content.Context;

import androidx.room.Room;

import com.example.bequiet.model.dataclasses.AreaRule;
import com.example.bequiet.model.dataclasses.Rule;
import com.example.bequiet.model.dataclasses.WlanRule;
import com.example.bequiet.presenter.HomePagePresenter;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private final AppDatabase dbRef;
    private final Context context;

    public Database(Context context) {
        this.context = context;
        dbRef = Room.databaseBuilder(context,
                AppDatabase.class, "rules").build();
    }


    public void addRuleIntoDb(Rule rule) {
        Thread thread = new Thread(() -> {
            if (rule instanceof AreaRule) {
                dbRef.ruleDAO().insertAreaRule((AreaRule) rule);
            } else if (rule instanceof WlanRule) {
                dbRef.ruleDAO().insertWlanRule((WlanRule) rule);
            }
            dbRef.close();
        });
        thread.start();
    }

    public void getAllRulesInCallback(RuleListCallback r) {
        Thread thread = new Thread(() -> {
            List<Rule> rules = new ArrayList<>();
            List<WlanRule> wlanRules = dbRef.ruleDAO().loadAllWlanRules();
            List<AreaRule> areaRules = dbRef.ruleDAO().loadAllAreaRules();
            rules.addAll(wlanRules);
            rules.addAll(areaRules);
            r.loaded(rules);
            dbRef.close();
        });
        thread.start();
    }

    public void getAreaRulesInCallback(AreaRuleListCallback r) {
        Thread thread = new Thread(() -> {
            List<AreaRule> areaRules = dbRef.ruleDAO().loadAllAreaRules();
            r.loaded(areaRules);
            dbRef.close();
        });
        thread.start();
    }

    public void getWlanRulesInCallback(WlanRuleListCallback r) {
        Thread thread = new Thread(() -> {
            List<WlanRule> wlanRules = dbRef.ruleDAO().loadAllWlanRules();
            r.loaded(wlanRules);
            dbRef.close();
        });
        thread.start();
    }

    public void updateDBAreaRule(AreaRule a) {
        Thread thread = new Thread(() -> {
            dbRef.ruleDAO().updateAreaRule(a);
            dbRef.close();
        });
        thread.start();
    }

    public void updateDBWlanRule(WlanRule w) {
        Thread thread = new Thread(() -> {
            dbRef.ruleDAO().updateWlanRule(w);
            dbRef.close();
        });
        thread.start();
    }

    public void deleteWifiRule(WlanRule wlanRule, HomePagePresenter homePagePresenter) {
        Thread thread = new Thread(() -> {
            dbRef.ruleDAO().deleteWlanRule(wlanRule);
            dbRef.close();
            homePagePresenter.getRulesAndDraw(context);
        });
        thread.start();
    }

    public void deleteAreaRule(AreaRule areaRule, HomePagePresenter homePagePresenter) {
        Thread thread = new Thread(() -> {
            dbRef.ruleDAO().deleteAreaRule(areaRule);
            dbRef.close();
            homePagePresenter.getRulesAndDraw(context);
        });
        thread.start();
    }
}
