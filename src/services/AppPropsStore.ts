import { injectable } from 'inversify';
import { makeAutoObservable, observable } from 'mobx';

@injectable()
export class AppPropsStore {
    public props: any = undefined;

    constructor() {
        makeAutoObservable(this, { props: observable.struct });
    }

    public setProps(props: any) {
        this.props = props;
    }
}
