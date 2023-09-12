import { injectable } from 'inversify';
import { observer } from 'mobx-react-lite';
import { StyleSheet, View } from 'react-native';
import { makeAutoObservable, observable } from 'mobx';
import { useDependencyInjector } from './container';
import { SnackbarProps } from '../Components/Snackbar';
import { randomString, createDeferredPromise } from '../utils';

const kResolve = Symbol('kResolve');
type SnackbarPromise<T extends SnackbarProps> = Promise<boolean> & Snackbar<T>;

export class Snackbar<T extends SnackbarProps> {
    public promise: SnackbarPromise<T>;
    public [kResolve]: (byAction: boolean) => void;
    public readonly id = randomString();

    constructor(public Component: React.FC<T>, public props: Omit<T, 'closeSnackbar'>, public close: (byAction: boolean) => void) {
        const { promise, resolve } = createDeferredPromise<boolean>();
        this.promise = Object.assign(promise, this);
        this[kResolve] = resolve;
    }
}

@injectable()
export class SnackbarStore {
    public readonly snackbarInstances = observable.map<string, Snackbar<any>>();

    constructor() {
        makeAutoObservable(this);
    }

    openSnack<T extends SnackbarProps>(Component: React.FC<T>, props: Omit<T, 'closeSnackbar'> = {} as any) {
        const snackbar = new Snackbar<T>(Component, props, (byAction: boolean) => {
            snackbar[kResolve](byAction);
            this.snackbarInstances.delete(snackbar.id);
        });

        this.snackbarInstances.set(snackbar.id, snackbar);
        return snackbar.promise;
    }
}

export const SnackbarWrapper = observer(() => {
    const snackbarStore = useDependencyInjector(SnackbarStore);

    if (snackbarStore.snackbarInstances.size === 0) {
        return null;
    }

    return (
        <View style={styles.snackbarWrapper}>
            {[...snackbarStore.snackbarInstances.values()].map(({ Component, id, close, props }) => (
                <Component key={id} {...props} closeSnackbar={close} />
            ))}
        </View>
    );
});
SnackbarWrapper.displayName = 'SnackbarWrapper';

const styles = StyleSheet.create({
    snackbarWrapper: {
        gap: 5,
        left: 0,
        right: 0,
        bottom: 10,
        zIndex: 100,
        position: 'absolute',
        paddingHorizontal: 10,
    },
});
