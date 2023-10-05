import { injectable } from 'inversify';
import { observer } from 'mobx-react-lite';
import { StyleSheet, View } from 'react-native';
import { makeAutoObservable, observable, runInAction } from 'mobx';
import { useDependencyInjector } from './container';
import { randomString, createDeferredPromise } from '../utils';

const kPromise = Symbol('kPromise');
const kResolve = Symbol('kResolve');
const kDialogInstances = Symbol('kDialogInstances');
export interface IDialogInstance<T extends {} = any> {
    closeDialog(value?: T): void;
}
type CloseDialog<T extends IDialogInstance> = Parameters<T['closeDialog']>[0];
export interface DialogPromise<T extends IDialogInstance> extends Promise<CloseDialog<T>>, Dialog<T> {}

export class Dialog<T extends IDialogInstance> {
    public [kPromise]: DialogPromise<T>;
    public [kResolve]: (value: CloseDialog<T>) => void;
    public readonly id = randomString();

    constructor(public Component: React.FC<T>, public props: Omit<T, 'closeDialog'>, public close: (value: CloseDialog<T>) => void) {
        const { promise, resolve } = createDeferredPromise<CloseDialog<T>>();
        this[kPromise] = Object.assign(promise, this);
        this[kResolve] = resolve;
    }
}

@injectable()
export class DialogStore {
    public readonly [kDialogInstances] = observable.map<string, Dialog<any>>();

    constructor() {
        makeAutoObservable(this);
    }

    public openDialog<T extends IDialogInstance>(Component: React.FC<T>, props: Omit<T, 'closeDialog'> = {} as any) {
        const dialog = new Dialog<T>(Component, props, (value: CloseDialog<T>) => {
            dialog[kResolve](value);
            runInAction(() => {
                this[kDialogInstances].delete(dialog.id);
            });
        });

        this[kDialogInstances].set(dialog.id, dialog);
        return dialog[kPromise];
    }
}

export const DialogWrapper = observer(() => {
    const dialogStore = useDependencyInjector(DialogStore);

    if (dialogStore[kDialogInstances].size === 0) {
        return null;
    }

    return (
        <View style={styles.dialogWrapper}>
            {[...dialogStore[kDialogInstances].values()].map(({ Component, id, close, props }) => (
                <Component key={id} {...props} closeDialog={close} />
            ))}
        </View>
    );
});
DialogWrapper.displayName = 'DialogWrapper';

const styles = StyleSheet.create({
    dialogWrapper: {
        gap: 5,
        left: 0,
        right: 0,
        bottom: 10,
        zIndex: 100,
        position: 'absolute',
        paddingHorizontal: 10,
    },
});
