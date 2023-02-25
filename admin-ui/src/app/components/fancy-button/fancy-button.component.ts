import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-fancy-button',
  templateUrl: './fancy-button.component.html',
  styleUrls: ['./fancy-button.component.scss']
})
export class FancyButtonComponent {

  @Input() backgroundText: string = "";
  @Input() buttonIcon: string = "add-reaction";
  @Output() buttonClicked: EventEmitter<void> = new EventEmitter();

  click() {
    this.buttonClicked.emit();
  }
}
