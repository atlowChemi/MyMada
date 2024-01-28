import { DependencyList, useEffect } from 'react';
import { Dimensions, PixelRatio } from 'react-native';

export const createDeferredPromise = <T = void>() => {
    let resolve!: (value: T | PromiseLike<T>) => void;
    let reject!: (reason?: any) => void;
    const promise = new Promise<T>((res, rej) => {
        resolve = res;
        reject = rej;
    });
    return { promise, resolve, reject };
};

const alphabet = 'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890';

export const randomString = (length = 16, source = alphabet) => {
    let result = '';
    for (let i = 0; i < length; i++) {
        const index = Math.floor(Math.random() * source.length);
        result += source[index];
    }
    return result;
};

export const roundLayoutSize = (size: number) => Math.round(size * 1000) / 1000;

const isNumber = (value: any): value is number => typeof value === 'number' && !Number.isNaN(value);

export const inRange = (input: number, min?: number, max?: number) => {
    if (!isNumber(input)) {
        throw new Error('input was not a number!');
    }
    if (min != null && !isNumber(min)) {
        throw new Error('min was not a number');
    }
    if (max != null && !isNumber(max)) {
        throw new Error('max was not a number');
    }
    min ??= undefined;
    max ??= undefined;
    if (typeof min === 'undefined' && typeof max === 'undefined') {
        return true;
    }
    let gt = true;
    let lte = true;
    if (typeof min === 'number') {
        gt = input > min;
    }
    if (typeof max === 'number') {
        lte = input <= max;
    }
    return gt && lte;
};

interface EffectFn<T> {
    (isMounted: () => boolean): T | undefined | PromiseLike<T>;
}
export function useAsyncEffect<T>(effect: EffectFn<T>, deps: DependencyList): void;
export function useAsyncEffect<T>(effect: EffectFn<T>, destroy: Function, deps: DependencyList): void;
export function useAsyncEffect<T>(effect: EffectFn<T>, destroy: Function | DependencyList, deps?: DependencyList): void {
    useEffect(
        () => {
            let result: T | undefined;
            let mounted = true;
            const maybePromise = effect(() => mounted);

            Promise.resolve(maybePromise).then(value => {
                result = value;
            });

            return () => {
                mounted = false;

                if (typeof destroy === 'function') {
                    destroy(result);
                }
            };
        },
        typeof destroy === 'function' ? deps : destroy, // eslint-disable-line react-hooks/exhaustive-deps
    );
}

export const isTablet = () => {
    const pixelDensity = PixelRatio.get();
    const { width, height } = Dimensions.get('window');
    const adjustedWidth = width * pixelDensity;
    const adjustedHeight = height * pixelDensity;
    if (pixelDensity < 2 && (adjustedWidth >= 1000 || adjustedHeight >= 1000)) {
        return true;
    }
    return pixelDensity === 2 && (adjustedWidth >= 1920 || adjustedHeight >= 1920);
};
