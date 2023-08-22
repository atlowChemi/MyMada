import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import he from './he';
import en from './en';

const resources = {
    he,
    en,
};

i18n.use(initReactI18next).init({
    compatibilityJSON: 'v3', // Required for android
    resources,
    lng: 'he',
    interpolation: {
        escapeValue: false,
    },
});
export default i18n;
