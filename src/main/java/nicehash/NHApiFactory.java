package nicehash;

public class NHApiFactory {
    private static NHApi nhApi = null;

    // Singleton
    public static NHApi getInstance() {
        if (nhApi == null) {
            nhApi = new NHApiImpl();
        }

        return nhApi;
    }

    public static void setNhApi(NHApi nhApi) {
        NHApiFactory.nhApi = nhApi;
    }
}
