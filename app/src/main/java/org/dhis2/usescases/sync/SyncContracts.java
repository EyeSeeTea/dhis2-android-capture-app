package org.dhis2.usescases.sync;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.D2;

public class SyncContracts {

    public interface View extends AbstractActivityContracts.View{

        void saveTheme(Integer themeId);

        void saveFlag(String s);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init(View view, D2 d2);

        void sync();

        void syncReservedValues();

        void getTheme();

        void scheduleSync(int metaTime, int dataTime);
    }
}
