import { NativeModules } from 'react-native';
import { AllowedLanguages } from '../localization';

const { LocalizationSettings, I18nManager } = NativeModules;

/** @returns Language in IETF BCP 47 format (like 'en-US') */
export function getLanguage(): AllowedLanguages {
    LocalizationSettings.getLanguage();
    return LocalizationSettings.language.split(/[_-]/)[0];
}

export function setLanguage(language: string): void {
    LocalizationSettings.setLanguage(language);
    I18nManager.forceRTL(language === 'he');
}

/** i18next language detector */
export const LanguageDetector: I18nLanguageDetectorModule = {
    type: 'languageDetector',
    init: () => {},
    detect: () => getLanguage(),
    cacheUserLanguage(lng) {
        setLanguage(lng);
    },
};

interface I18nLanguageDetectorModule {
    type: 'languageDetector';
    init?(): void;
    detect(): string | readonly string[] | undefined;
    cacheUserLanguage?(lng: string): void;
}
