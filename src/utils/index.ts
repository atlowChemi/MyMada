import { DependencyList, useEffect } from 'react';

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

export const randomString = (length = 16) => {
    let result = '';
    for (let i = 0; i < length; i++) {
        const index = Math.floor(Math.random() * alphabet.length);
        result += alphabet[index];
    }
    return result;
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
