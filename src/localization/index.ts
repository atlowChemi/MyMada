import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { I18nManager } from 'react-native';
import he from './he';
import en from './en';

const resources = {
    he,
    en,
};

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
    lng: 'he',
    interpolation: {
        escapeValue: false,
    },
});
export default i18n;
export type TranslationKeys = Join<PathsToStringProps<typeof he>, ':'>;
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
