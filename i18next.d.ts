// import the original type declarations
import 'i18next';
// import all namespaces (for the default language, only)
import he from './src/localization/he';
import en from './src/localization/he';

declare module 'i18next' {
    // Extend CustomTypeOptions
    interface CustomTypeOptions {
        // custom resources type
        resources: {
            he: typeof he;
            en: typeof en;
        };
        compatibilityJSON: 'v3';
        jsonFormat: 'v3';
        // other
    }
}
