import { Container, interfaces, METADATA_KEY } from 'inversify';
import { createContext, useContext, useEffect, useMemo } from 'react';

export const container = new Container({
    autoBindInjectable: true,
    defaultScope: 'Singleton',
});

export const DIContext = createContext<{ container: Container | null }>({ container: null });
export const DependencyInjectionProvider: React.FC<{ container: Container; children: React.ReactNode }> = props => (
    <DIContext.Provider value={{ container: props.container }}>{props.children}</DIContext.Provider>
);
DependencyInjectionProvider.displayName = 'DependencyInjectionProvider';

export const useDependencyInjector = <T extends {}>(identifier: interfaces.ServiceIdentifier<T>) => {
    const { container: _container } = useContext(DIContext);
    if (!_container) {
        throw new Error('useDependencyInjector missing DI container in context');
    }
    return _container.get<T>(identifier);
};

export const useLocalStoreWithInjectedDependencies = <T extends {}>(Class: interfaces.Newable<T>) => {
    const { container: _container } = useContext(DIContext);
    if (!_container) {
        throw new Error('useLocalStoreWithInjectedDependencies missing DI container in context');
    }
    const store = useMemo(() => {
        // Fallback to empty array - as this will be undefined when no constructor or no args in constructor.
        const dependencyClasses: interfaces.Newable<any>[] = Reflect.getMetadata(METADATA_KEY.DESIGN_PARAM_TYPES, Class) || [];
        const dependencyConstructorParams = dependencyClasses.map((c, index) => {
            try {
                return container.get(c);
            } catch (e) {
                const errorMessage = `Failed to find dependency for: ${c.name}, param index:${index}, dependencyClasses: ${String(dependencyClasses)}`;
                // Crashlytics
                console.error(errorMessage, e);
                return undefined;
            }
        });

        return new (Class as any)(...dependencyConstructorParams) as T;
    }, [Class]);

    useEffect(() => () => (store as any).dispose?.(), [store]);
    return store;
};
