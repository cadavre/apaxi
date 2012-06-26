
package pro.jazzy.paxi.entity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class My extends Activity {

    String TAG = "test";

    @Override
    protected void onResume() {

        super.onResume();

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        Route myRoute = new Route(preferences);

        ModeChange siedem = new ModeChange(Route.MIXED_MODE);
        myRoute.changeMode(siedem);

        Member driver = new Member("Driver");
        myRoute.memberIn(driver);

        Member freshman = new Member("Passenger");
        myRoute.memberIn(freshman);

        myRoute.addDistance(15000);

        Member oldman = new Member("Autostop");
        myRoute.memberIn(oldman);

        myRoute.addDistance(5000);

        ModeChange szesc = new ModeChange(Route.HIGHWAY_MODE);
        myRoute.changeMode(szesc);

        myRoute.addDistance(5000);

        Payment pajmi = new Payment(30f);
        myRoute.addPayment(pajmi);

        myRoute.addDistance(10000);

        MemberOut freshass = new MemberOut("Passenger");
        myRoute.memberOut(freshass);

        myRoute.addDistance(5000);

        ModeChange osiem = new ModeChange(Route.CITY_MODE);
        myRoute.changeMode(osiem);

        myRoute.addDistance(5000);

        MemberOut freshaass = new MemberOut("Autostop");
        myRoute.memberOut(freshaass);

        Payment pajmik = new Payment(3f);
        myRoute.addPayment(pajmik);

        myRoute.addDistance(5000);

        MemberOut fresharss = new MemberOut("Driver");
        myRoute.memberOut(fresharss);

    }

}
