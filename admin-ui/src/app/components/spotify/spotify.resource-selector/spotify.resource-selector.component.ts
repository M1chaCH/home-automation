import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {SpotifyService} from "../../../services/spotify.service";
import {SpotifyResourceDTO} from "../../../dtos/spotify/SpotifyResourceDTO";
import {firstValueFrom} from "rxjs";

@Component({
  selector: 'app-spotify-resource-selector',
  templateUrl: './spotify.resource-selector.component.html',
  styleUrls: ['./spotify.resource-selector.component.scss']
})
export class SpotifyResourceSelectorComponent implements OnInit{
  private readonly MAX_SHOWN_RESOURCES: number = 8;

  @Input() selectedResource: SpotifyResourceDTO | undefined;
  @Input() small: boolean = false;
  @Output() selectedResourceChange: EventEmitter<SpotifyResourceDTO> = new EventEmitter<SpotifyResourceDTO>();

  filteredResources: SpotifyResourceDTO[] = [];
  query: string = "";
  open: boolean = false;

  private resources: SpotifyResourceDTO[] = [];

   constructor(
    private service: SpotifyService,
  ) { }

  async ngOnInit(): Promise<void> {
     this.resources = await firstValueFrom(this.service.fetchResources());
     this.filteredResources = this.resources.slice(0, this.MAX_SHOWN_RESOURCES);
  }

  @HostListener('document:keydown.escape')
  escEventListener() {
    this.closeDialog();
  }

  @HostListener('document:keydown.enter',  ['$event'])
  enterEventListener(event: KeyboardEvent) {
    event.preventDefault();
  }

  toggleDialog(): void {
     if(this.open) {
       this.closeDialog()
     } else {
       this.open = true;
     }
  }

  queryChanged(query: string): void {
     query = query.toLowerCase();
     this.filteredResources = this.resources.filter(r => r.name.toLowerCase().includes(query)
       || r.href.includes(query));
     this.filteredResources = this.filteredResources.slice(0, this.MAX_SHOWN_RESOURCES);
  }

  resourceSelected(resource?: SpotifyResourceDTO): void {
     if(!resource){
       if(this.filteredResources.length < 1)
         return;
       resource = this.filteredResources[0];
     }

     this.selectedResource = resource;
     this.selectedResourceChange.emit(resource);
     this.closeDialog();
  }

  removeResource(): void {
     this.selectedResource = undefined;
     this.selectedResourceChange.emit(undefined);
     this.closeDialog();
  }

  closeDialog() {
    this.query = "";
    this.queryChanged("");
    this.open = false;
  }
}
