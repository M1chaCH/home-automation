import {Component, EventEmitter, Input, Output} from '@angular/core';
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";

@Component({
  selector: 'app-light-config-preview',
  templateUrl: './light-config-preview.component.html',
  styleUrls: ['./light-config-preview.component.scss']
})
export class LightConfigPreviewComponent {
  @Input() lightConfig!: LightConfigDTO;
  @Output() previewClicked: EventEmitter<void> = new EventEmitter<void>();
}
