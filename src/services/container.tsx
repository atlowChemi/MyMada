import { Container, interfaces } from 'inversify';
import { createContext, useContext } from 'react';

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
