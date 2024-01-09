import { makeAutoObservable } from 'mobx';

interface LinkedListOptions {
    double?: boolean;
    deepObservable?: boolean;
}

const kPrev = Symbol('prev');
const kNext = Symbol('next');

class Node<T> {
    public [kPrev]: Node<T> | null = null;
    public [kNext]: Node<T> | null = null;

    constructor(public readonly value: T, { deepObservable }: LinkedListOptions = {}) {
        if (deepObservable) {
            makeAutoObservable(this, {});
        }
    }

    public get prev() {
        return this[kPrev];
    }

    public get next() {
        return this[kNext];
    }
}
export type NodeOf<T> = Node<T>;

export class LinkedList<T extends {}> {
    private _length = 0;
    private _head: Node<T> | null = null;
    private _tail: Node<T> | null = null;

    constructor(data?: T | null | undefined, private readonly options: LinkedListOptions = {}) {
        if (data) {
            this.add(data);
        }
        makeAutoObservable(this, { [Symbol.iterator]: false, reversed: false, nodes: false, reversedNodes: false });
    }

    public get isEmpty() {
        return this._head === null;
    }

    public get length() {
        return this._length;
    }

    public get head() {
        return this._head;
    }

    public get tail() {
        return this._tail;
    }

    public get array() {
        const explicitUpdate = this.length; // eslint-disable-line @typescript-eslint/no-unused-vars
        return Array.from(this);
    }

    public get reversedArray() {
        const explicitUpdate = this.length; // eslint-disable-line @typescript-eslint/no-unused-vars
        return Array.from(this.reversed());
    }

    public get nodeArray() {
        const explicitUpdate = this.length; // eslint-disable-line @typescript-eslint/no-unused-vars
        return Array.from(this.nodes());
    }

    public get reversedNodeArray() {
        const explicitUpdate = this.length; // eslint-disable-line @typescript-eslint/no-unused-vars
        return Array.from(this.reversedNodes());
    }

    public clear = () => {
        this._length = 0;
        this._head = null;
        this._tail = null;
    };

    public add = (data: T | T[]) => {
        if (Array.isArray(data)) {
            data.forEach(this.innerAdd);
            return;
        }
        this.innerAdd(data);
    };

    public remove = (data: T) => {
        let current = this.head;
        let prev = null;

        while (current) {
            // Current node is not the node we are looking for, move to the next node
            if (current.value !== data) {
                prev = current;
                current = current.next;
                continue;
            }
            // The node we are looking for is the head node, which means it has no previous node,
            // so we just set the head to the next node, and if the tail is the same as the head, clean that reference as well.
            if (prev === null) {
                this._head = current.next;
                if (this.options.double && this._head) {
                    this._head[kPrev] = null;
                }
                if (this.tail === current) {
                    this._tail = null;
                }
            } else {
                prev[kNext] = current.next;
                if (this.options.double && current.next) {
                    current.next[kPrev] = prev;
                }
                if (this.tail === current) {
                    this._tail = prev;
                }
            }
            this._length--;
            return current.value;
        }
        return false;
    };

    public *[Symbol.iterator]() {
        let current = this.head;
        while (current) {
            yield current.value;
            current = current.next;
        }
    }

    public *reversed() {
        let current = this.tail;
        while (current) {
            yield current.value;
            current = current.prev;
        }
    }

    public *nodes() {
        let current = this.head;
        while (current) {
            yield current;
            current = current.next;
        }
    }

    public *reversedNodes() {
        let current = this.tail;
        while (current) {
            yield current;
            current = current.prev;
        }
    }

    public static fromArray = <U extends {}>(array: U[], options?: LinkedListOptions) => {
        const list = new LinkedList<U>(undefined, options);
        list.add(array);
        return list;
    };

    private innerAdd = (data: T) => {
        const node = new Node(data, this.options);
        try {
            if (this.isEmpty) {
                this._head = node;
                return;
            }
            const currentTail = this.tail!;
            currentTail[kNext] = node;
            if (this.options.double) {
                node[kPrev] = currentTail;
            }
        } finally {
            this._tail = node;
            this._length++;
        }
    };
}
