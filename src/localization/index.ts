import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { I18nManager, NativeModules } from 'react-native';
import he from './he';
import en from './en';
import { container } from '../services/container';
import { SharedPreferencesStore } from '../services/SharedPreferencesStore';
import { fromPromise } from 'mobx-utils';

const resources = {
    he,
    en,
};
const allowedLanguages = Object.keys(resources);

type PathsToStringProps<T> = T extends string ? [] : { [K in Extract<keyof T, string>]: [K, ...PathsToStringProps<T[K]>] }[Extract<keyof T, string>];
type Join<T extends string[], D extends string> = T extends []
    ? never
    : T extends [infer F]
    ? F
    : T extends [infer F, ...infer R]
    ? F extends string
        ? `${F}${D}${Join<Extract<R, string[]>, D>}`
        : never
    : string;

i18n.use(initReactI18next).init({
    compatibilityJSON: 'v3', // Required for android
    resources,
    fallbackLng: 'he',
    nonExplicitSupportedLngs: true,
    appendNamespaceToMissingKey: true,
    interpolation: {
        escapeValue: false,
    },
});
export default i18n;
export type TranslationKeys = Join<PathsToStringProps<typeof he>, ':'>;
export const languageSetup = fromPromise(setupLanguage());
async function setupLanguage() {
    const sharedPreferencesStore = container.get(SharedPreferencesStore);
    const { lng } = await sharedPreferencesStore.settingsChannel;
    let language = lng || NativeModules.I18nManager.localeIdentifier.split(/_-/)[0];
    if (!allowedLanguages.includes(language)) {
        language = 'he';
    }
    if (language !== i18n.resolvedLanguage) {
        return await setLanguage(language as keyof typeof resources);
    }
}
export function setLanguage(lang: keyof typeof resources) {
    return new Promise<void>((res, rej) => {
        i18n.changeLanguage(lang, err => {
            if (err) {
                return rej(err);
            }
            I18nManager.forceRTL(lang === 'he');
            res();
        });
    });
}
