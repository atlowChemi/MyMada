import i18n from 'i18next';
import he from './he';
import en from './en';
import { initReactI18next } from 'react-i18next';
import { LanguageDetector } from '../services/LanguageService';

const resources = {
    he,
    en,
};
export type AllowedLanguages = keyof typeof resources;

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

i18n.use(LanguageDetector)
    .use(initReactI18next)
    .init({
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
