package org.gms.provider;

public class LocalizedDataProvider implements DataProvider {
    private final DataProvider localized;
    private final DataProvider fallback;

    public LocalizedDataProvider(DataProvider localized, DataProvider fallback) {
        this.localized = localized;
        this.fallback = fallback;
    }

    @Override
    public Data getData(String path) {
        Data data = localized.getData(path);
        // 语言目录里没有该 XML 时，使用原始 WZ，避免为了少量翻译复制整包资源。
        return data != null ? data : fallback.getData(path);
    }

    @Override
    public DataDirectoryEntry getRoot() {
        // 导航目录保持原始 WZ 的完整文件树，确保未翻译资源仍然可枚举。
        return fallback.getRoot();
    }
}
