package development.dreamcatcher.moneyboxlight.Data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;
import javax.inject.Singleton;
import development.dreamcatcher.moneyboxlight.ApiService.ApiClient;
import development.dreamcatcher.moneyboxlight.ApiService.ApiUtils;


@Singleton
public class DataRepository {

    public static DataRepository dataRepository;
    public static ApiClient apiClient;

    private InternalDatabase internalDatabase;
    private String bearerToken;
    private Integer isaInvestorProductId;
    private Integer giaInvestorProductId;

    public LiveData<AccountData> isaAccountData() {

        final MutableLiveData<AccountData> data = new MutableLiveData<>();
        final LiveData<AccountData> account;

        // Check if the account data is stored in the internal DB.
        if (!isIsaAccountStored()) {

            // If not - call API to fetch the data from a server (using separate thread),
            // update internal database, and return this fetched data.
            ApiUtils.requestAccountData(1, apiClient, data);
            Log.d("Flag019: ", "01");
            return data;
        }

        // If the data is already stored - return the version from internal DB.
        account = dataRepository.internalDatabase.accountDao().loadIsaAccount();
        Log.d("Flag019: ", "02");

        return account;
    }

    public LiveData<AccountData> giaAccountData() {

        final MutableLiveData<AccountData> data = new MutableLiveData<>();
        final LiveData<AccountData> account;

        if (!isGiaAccountStored()) {
            ApiUtils.requestAccountData(2, apiClient, data);
            return data;
        }

        account = dataRepository.internalDatabase.accountDao().loadGiaAccount();
        return account;
    }


    public static void initialize(Context context) {

        if (dataRepository == null)
            dataRepository = new DataRepository();

        if (dataRepository.internalDatabase == null)
            dataRepository.internalDatabase = InternalDatabase.getInternalDatabase(context);

        if (apiClient == null)
            apiClient = ApiUtils.getAPIService();
    }


    public void add10ToMoneyBox(Integer accountType, Integer investorProductId) {
        ApiUtils.requestAdd10MoneyBox(accountType, apiClient, investorProductId);
    }

    public void fetchBearerToken() {
        ApiUtils.requestLogin(apiClient);
    }
    public String getBearerToken() { return bearerToken; }
    public void setBearerToken(String bearerToken) { this.bearerToken = bearerToken; }

    public void setIsaInvestorProductId(Integer isaInvestorProductId) { this.isaInvestorProductId = isaInvestorProductId; }
    public Integer getIsaInvestorProductId() { return this.isaInvestorProductId; }

    public void setGiaInvestorProductId(Integer giaInvestorProductId) { this.giaInvestorProductId = giaInvestorProductId; }
    public Integer getGiaInvestorProductId() { return this.giaInvestorProductId; }

    public InternalDatabase getInternalDatabase() { return internalDatabase; }

    public static Boolean isIsaAccountStored() {
        return (dataRepository.getInternalDatabase().accountDao().getIsaAccountsAmount() != 0);
    }

    public static Boolean isGiaAccountStored() {
        return (dataRepository.getInternalDatabase().accountDao().getGiaAccountsAmount() != 0);
    }

    public static void updateStoredAccount(Integer accountType, AccountData accountData) {

        // 1 - ISA Account Type
        // 2 - GIA Account Type

        AccountDao dao = dataRepository.getInternalDatabase().accountDao();

        if (accountType == 1) {
            if (isIsaAccountStored()) dao.update(accountData);
            else dao.save(accountData);
        }
        else {
            if (isGiaAccountStored()) dao.update(accountData);
            else dao.save(accountData);
        }
    }

    public boolean isLoggedIn() { return (bearerToken != null); }
}
