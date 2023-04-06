import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-hidden-input',
  templateUrl: './hidden-input.component.html',
  styleUrls: ['./hidden-input.component.scss']
})
export class HiddenInputComponent {
  @Input() placeholder: string = "";
  @Input() initialValue: string = "";
  @Input() lengthLimit: number = 50;

  @Output() saveRequest: EventEmitter<string> = new EventEmitter<string>();

  saveValue(e: Event) {
    // @ts-ignore
    const changedValue: string = e.target.value;
    this.saveRequest.emit(changedValue);
  }

  resetValue(e: Event): void {
    // @ts-ignore
    e.target.value = this.initialValue;
  }
}
