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
  @Output() selectedResourceChange: EventEmitter<SpotifyResourceDTO> = new EventEmitter<SpotifyResourceDTO>();

  filteredResources: SpotifyResourceDTO[] = [];
  query: string = "";

  private dialogElement!: HTMLDialogElement;
  private resources: SpotifyResourceDTO[] = [];
  private open: boolean = false;

   constructor(
    private service: SpotifyService,
  ) { }

  async ngOnInit(): Promise<void> {
     this.resources = await firstValueFrom(this.service.fetchResources());
     this.filteredResources = this.resources.slice(0, this.MAX_SHOWN_RESOURCES);
     this.dialogElement = document.getElementById("resources-dialog") as HTMLDialogElement;
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
       this.dialogElement.show();
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

  private closeDialog() {
    this.query = "";
    this.queryChanged("");
    this.dialogElement.close();
    this.open = false;
  }
}
